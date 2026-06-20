pipeline {
    agent {
        label 'docker'
    }

    environment {
        IMAGE_NAME = 'example-hello'
        GITEA_REGISTRY = 'gitea.albireo.me/crux'
        GITEA_CREDENTIAL_ID = 'gitea_albireo'
        FULL_IMAGE_NAME = "${GITEA_REGISTRY}/${IMAGE_NAME}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Determine Version') {
            steps {
                script {
                    def gitTag = sh(script: "git describe --exact-match --tags HEAD 2>/dev/null || echo ''", returnStdout: true).trim()
                    if (gitTag) {
                        env.IMAGE_TAG = gitTag
                    } else {
                        def branchName = env.BRANCH_NAME ?: sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                        branchName = branchName.replaceAll('/', '-')
                        env.IMAGE_TAG = "${branchName}-${env.BUILD_NUMBER}"
                    }
                    echo "Image tag: ${env.IMAGE_TAG}"
                }
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                echo 'Building and pushing multi-arch Docker image (amd64 + arm64)...'
                script {
                    docker.withRegistry("https://${GITEA_REGISTRY}", "${GITEA_CREDENTIAL_ID}") {
                        sh """
                            docker buildx create --name multiarch --use || docker buildx use multiarch
                            docker buildx build \
                                --platform linux/amd64,linux/arm64 \
                                --tag ${FULL_IMAGE_NAME}:${IMAGE_TAG} \
                                --tag ${FULL_IMAGE_NAME}:latest \
                                --push \
                                .
                        """
                    }
                }
            }
        }

        stage('Verify Push') {
            steps {
                echo "Pushed ${FULL_IMAGE_NAME}:${IMAGE_TAG} and :latest"
            }
        }
    }

    post {
        always {
            sh 'docker buildx prune -f || true; docker image prune -f || true'
        }
    }
}
