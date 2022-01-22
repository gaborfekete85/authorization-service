#/bin/bash

#Build
#./gradlew clean build
# npm run build
gradle clean build

#Docker image push
docker image build -t security/authorization-service .
docker tag security/authorization-service:latest localhost:5000/authorization-service:latest
docker push localhost:5000/authorization-service:latest

#docker login

#	     --set image.repository=gabendockerzone/booking-ui \
#	     --set image.tag=1.0.0 \


#Helm Redeploy
cd ../../pipeline/K8S
helm delete authorization-service -n security
helm install -f service/values-authorization-service-dev.yaml authorization-service ./service -n security