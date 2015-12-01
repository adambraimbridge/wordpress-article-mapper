FROM up-registry.ft.com/coco/dropwizardbase

ADD . /

RUN apk --update add git \
 && HASH=$(git log -1 --pretty=format:%H) \
 && BUILD_NUMBER=$(cat ./buildnum.txt) \
 && BUILD_URL=$(cat ./buildurl.txt) \
 && mvn install -Dbuild.git.revision=$HASH -Dbuild.git.revision=$HASH -Dbuild.number=$BUILD_NUMBER -Dbuild.url=$BUILD_URL -Djava.net.preferIPv4Stack=true \
 && rm -f target/wordpress-article-transformer-*sources.jar \
 && mv target/wordpress-article-transformer-*.jar /app.jar \
 && mv wordpress-article-transformer.yaml /config.yaml \
 && apk del git \
 && rm -rf /var/cache/apk/* \
 && rm -rf /root/.m2/*

EXPOSE 8080 8081

CMD echo -e "wordpress.contentApi.key=$WORDPRESS_CONTENT_API_KEY" > /credentials.properties && \
    chmod 400 /credentials.properties && \
    java -Ddw.server.applicationConnectors[0].port=8080 \
     -Ddw.server.adminConnectors[0].port=8081 \
     -Ddw.logging.appenders[0].logFormat="%-5p [%d{ISO8601, GMT}] %c: %X{transaction_id} %replace(%m%n[%thread]%xEx){'\n', '|'}%nopex%n" \
     -Ddw.healthCheckWordPressConnections[0].hostName="ftalphaville.ft.com" \
     -Ddw.healthCheckWordPressConnections[1].hostName="blogs.ft.com" \
     -Ddw.healthCheckWordPressConnections[2].hostName="blogs.ft.com" \
     -Ddw.healthCheckWordPressConnections[2].path="/photo-diary/api/get_recent_posts/" \
     -jar app.jar server config.yaml
