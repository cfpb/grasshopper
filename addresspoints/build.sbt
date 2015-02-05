enablePlugins(JavaServerAppPackaging)

maintainer in Linux := "John Smith <john.smith@example.com>"

packageSummary in Linux := "Address point geocoder"

packageDescription := "Grasshopper address point Geocoding Service"

daemonUser in Linux := normalizedName.value         // user which will execute the application

daemonGroup in Linux := (daemonUser in Linux).value // group which will execute the application

maintainer in Docker := "John Smith <john.smith@example.com>"

//daemonUser in Docker := normalizedName.value // user in the Docker image which will execute the application (must already exist)


// WARNING: DOING THIS TO GET IT WORKING (base Docker image doesn't have a lot of users to choose from. DON'T DO THIS IN PRODUCTION)
daemonUser in Docker := "root"

dockerBaseImage := "dockerfile/java" // Docker image to use as a base for the application image

dockerExposedPorts in Docker := Seq(8080) // Ports to expose from container for Docker container linking

