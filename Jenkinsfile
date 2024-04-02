pipeline {
    agent any
    tools {
        jdk "jdk17"
        maven "M3"
    }
    //
    environment {
        AWS_CREDENTIAL_NAME = "AWSCredentials"
        REGION = "ap-northeast-2"
        DOCKER_IMAGE_NAME = "std04-spring-petclinic"
        ECR_REPOSITORY = "257307634175.dkr.ecr.ap-northeast-2.amazonaws.com"
        ECR_DOCKER_IMAGE = "${ECR_REPOSITORY}/${DOCKER_IMAGE_NAME}"
    }
    
    stages {
        stage('Git clone') {
            steps {
                echo 'Git clone'
                git url: 'https://github.com/taebeen03/spring-petclinic.git',
                    branch: 'efficient-webjars', credentialsId: 'GitCredentials'
            }
            post {
                success {
                    echo 'Git Clone Success!!'
                }
                failure {
                    echo 'Git Clone Fail'
                }
            }
        }
        // 여기부터 새로 추가
        stage('Format Code') {
            steps {
                echo 'Formatting Code'
                // 포매팅 위반을 자동으로 수정합니다.
                sh 'mvn spring-javaformat:apply'
            }
        }

        stage('Maven Build') {
            steps {
                echo 'Maven Build'
                // 테스트 실패를 무시하는 대신에 포매팅을 수정한 후 정상적으로 빌드를 수행합니다.
                sh 'mvn clean package'
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml'
                }
            }
        }
        
        //stage('Maven Build') {
        //    steps {
        //        echo 'Maven Build'
        //        sh 'mvn -Dmaven.test.failure.ignore=true clean package'
        //    }
        //    post {
        //        success {
        //            junit 'target/surefire-reports/**/*.xml'
        //        }
        //    }
        //}

        stage ('Docker Image Build') {
            steps {
                echo 'Docker Image Build'
                dir("${env.WORKSPACE}") {
                    sh """
                        docker build -t $ECR_DOCKER_IMAGE:$BUILD_NUMBER .
                        docker tag $ECR_DOCKER_IMAGE:$BUILD_NUMBER $ECR_DOCKER_IMAGE:latest
                    """
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                echo "Push Docker Image to ECR"
                script{
                    sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
                    docker.withRegistry("https://${ECR_REPOSITORY}", "ecr:${REGION}:${AWS_CREDENTIAL_NAME}") {
                        docker.image("${ECR_DOCKER_IMAGE}:${BUILD_NUMBER}").push()
                        docker.image("${ECR_DOCKER_IMAGE}:latest").push()
                    }
                }
            }
        }
        stage('Clean Up Docker Images on Jenkins Server') {
            steps {
                echo 'Cleaning up unused Docker images on Jenkins server'
                sh "docker image prune -f -a"
            }
        }
        stage('Upload to S3') {
            steps {
                echo 'Upload to S3'
                dir("${env.WORKSPACE}") {
                    sh 'zip -r deploy.zip ./deploy appspec.yml'
                    withAWS(region:"${REGION}", credentials:"${AWS_CREDENTIAL_NAME}") {
                        s3Upload(file:"deploy.zip", bucket:"std04-codedeploy-bucket")
                    }
                    sh 'rm -rf ./deploy.zip'
                }
            }
        }
        stage('Codedeploy Workload') {
            steps {
               echo "create Codedeploy group"   
                sh '''
                    aws deploy create-deployment-group \
                    --application-name std04-code-deploy \
                    --auto-scaling-groups std04-asg \
                    --deployment-group-name std04-code-deploy-${BUILD_NUMBER} \
                    --deployment-config-name CodeDeployDefault.OneAtATime \
                    --service-role-arn arn:aws:iam::257307634175:role/std04-codedeploy-service-role
                    '''
                echo "Codedeploy Workload"   
                sh '''
                    aws deploy create-deployment --application-name std04-code-deploy \
                    --deployment-config-name CodeDeployDefault.OneAtATime \
                    --deployment-group-name std04-code-deploy-${BUILD_NUMBER} \
                    --s3-location bucket=std04-codedeploy-bucket,bundleType=zip,key=deploy.zip
                    '''
                    sleep(10) // sleep 10s
            }
        }
        
    }
}
