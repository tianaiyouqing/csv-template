pipeline {
    agent {
        docker {
          image 'hub.c.163.com/library/maven:3.5-jdk-9'
          args '-v /root/.m2:/root/.m2'
        }
    }
    parameters {
        string defaultValue: '', description: '', name: 'test', trim: false
    }
    stages {
        script{
            def mvnRep = checkout scm;
            sh "echo 'gitCommit:'${mvnRep.GIT_COMMIT}"
            sh "echo 'gitBranch:'"
            sh "echo 'imageTag:'"
        }
        stage('echo') {
            steps {
                script {
                    def mvnRep = checkout scm;
                    sh "echo 'gitCommit:'${mvnRep.GIT_COMMIT}"
                    sh "echo 'gitBranch:'"
                    sh "echo 'imageTag:'"
                }
            }
        }
        stage("input") {
            input {
                message "是否构建?"
                ok "是的，开始构建"
                submitter "111"
                parameters {
                    string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
                }
            }
            steps {
                echo "Hello, ${PERSON}, nice to meet you."
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
    }

    post {
        success {
            echo '---------------构建完成---------------'
        }
    }


}