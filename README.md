# Sentinel - Anomaly Detection with Akka and Scala  

## Introduction
The anomaly detection scala package is a project aim to detect anomalies in time series. 
The code is based on the twitter R implementation of the algorithm that can 
found [here](https://github.com/twitter/AnomalyDetection)
I'll invite you to jump and read about the algorithm based **Seasonal Hybrid ESD (S-H-ESD)** 
In this project I've looking to extents the R implementation to a more production environment 
like Scala and Akka

## Architecture
The project is managed by actor model. not that this add something but it's more something 
that permit to use the code base into a multiThreaded application. 

### TimeSeries Providers
 The `TimeSeriesProvider` is used to provide a valid time series to the detector. We provide a default implementation
 based on CVS file. You can develop your own `TimeSeriesProvider` there is a lot of job to do here 
 to make the provider more flexible and adapt it to all requirements.
  
### AnomalyDetectorActor
This actor can receive 2 messages
 * `TaskNow` used to fire the detector at specific interval
 * `DetectAnomalies` used to call the detector by providing the time series into the message itself
 
### The logger
This is a very simple layer to log the results of the detector. We provide a sample that simply write 
the result in Log File. But you can implement something sending a message to your monitoring platform
 for example
 
 
  
  