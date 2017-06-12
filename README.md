# Sentinel - Anomaly Detection with Akka and Scala  

## Introduction
The anomaly detection scala package is a project aim to detect anomalies in time series. 
The code is based on the twitter R implementation of the algorithm that can 
found [here](https://github.com/twitter/AnomalyDetection)
I'll invite you to jump and read about the algorithm based **Seasonal Hybrid ESD (S-H-ESD)** 
In this project I've looking to replace the R implementation by scala to a more 'production' 
environment

## Architecture
The project is managed by Akka actor model it's not really changing something thus it can be used   
also as example to work with akka. More over with you can use this code in a multiThreaded application like 
web application etc... 

### TimeSeries Providers
 The `TimeSeriesProvider` trait is used to provide a valid time series to the detector. 
 We provide a default implementation based on CVS file. You can develop your own `TimeSeriesProvider` 
 there is a lot of work to do here to make this provider more flexible 
 so it can be adapted to all requirements.
  
### AnomalyDetectorActor
This actor can receive 2 messages
 * `TaskNow` used to fire the detector at specific interval. need to use a specific time series provider
 * `DetectAnomalies` used to call the detector by providing the time series into the message itself.
 
 This actor can be spawned to multiple instance and call in parallel (multiThread)  
 
### The logger
This is a very simple layer to log the results of the detector. We provide a sample that simply write 
the result in Log File. But you can implement something sending a message to your monitoring platform
 for example
 
 
  
  