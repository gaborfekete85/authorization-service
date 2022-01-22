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
       sh "./gradlew clean build"
       app = docker.build("gabendockerzone/authorization-service")
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
			string(name: 'DOCKERTAG', value: "${env.git_hash[0..7]}"),
			string(name: 'APPLICATION_NAME', value: "authorization-service")
		]
        }
}
