run:
	java -jar build/libs/bird-assignment-tmp-0.1-all.jar

clean:
	./gradlew clean

build-all:
	./gradlew clean generateBatchschedulerJooqSchemaSource build -x test

build-inc:
	./gradlew build -x test

test:
	rm -rf build/test-results
	./gradlew test
