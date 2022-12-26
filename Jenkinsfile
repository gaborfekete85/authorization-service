node {
    def app
    
    parameters {
        string(name: 'git_hash', defaultValue: 'latest')
    }

    stage('Clone repository') {
      

        def scmVars = checkout scm
        env.git_hash = scmVars.GIT_COMMIT
    }

    stage('Build image') {
       environment {
	   DOCKER_USERNAME: gabendockerzone
	   DOCKER_PASSWORD: gD5Abb421
       }
       withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
           sh "chmod +x gradlew"
           sh "./gradlew clean build"
           app = docker.build("gabendockerzone/authorization-service")
       }
    }

    stage('Test image') {
  

        app.inside {
            sh 'echo "Tests passed"'
        }
    }

    stage('Push image') {
        
        docker.withRegistry('https://registry.hub.docker.com', 'dockerhub') {
            app.push("${env.git_hash[0..7]}")
        }
    }
    
    stage('Trigger ManifestUpdate') {
                echo "triggering updatemanifestjob"
                build job: 'updatemanifest', parameters: [
			string(name: 'APPLICATION_NAME', value: "${env.git_hash[0..7]}"),
			string(name: 'DOCKERTAG', value: "${env.git_hash[0..7]}")
		]
        }
}
