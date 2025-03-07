build:
	docker build \
		--platform linux/amd64 \
		--no-cache \
		--build-arg CONFIG_FILE=fnsClient.prod.conf \
		-t fns-client:latest \
		-t us-east1-docker.pkg.dev/notamify-staging/notamify-notam-api/fns-client:v1 \
		.

publish:
	docker push us-east1-docker.pkg.dev/notamify-staging/notamify-notam-api/fns-client:v1
