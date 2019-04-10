# Ascend Android SDK

#### Vocabulary

   **Participant:** The end user of the application, the individual who's actions are being recorded in the experiment.
   **Allocation:** The set of configurations that have been given to the participant, the values that are being
   experimented against.

For a complete example of how to use this SDK see our [example app](https://github.com/evolv-ai/ascend-android-sdk/blob/master/example/src/main/java/ai/evolv/ascend/example/MainActivity.java).

### Client Initialization

1. Build an AscendConfig instance.
    ```java
       AscendConfig config = AscendConfig.builder(<environment_id>).build();
    ```

2. Initialize the AscendClient.
    ```java
       AscendClient client = AscendClientFactory.init(config);
    ```
    
### Confirm the Allocation
    
1. Once the client has been initialized, confirm the participant into the experiment.
    ```java
       client.confirm();
    ```
    *Note: After the client has initialized, it is important to confirm the participant into the experiment. This action
     records the participant's allocation and sends the info back to Ascend.*

### Value Retrieval

1. Retrieve values from Ascend.
    ```java
       T value = client.get(<key_for_value>, <default_value>);
    ```
    
   *Note: The return value's type is decided by the provided default value's type. If there is an issue retrieving the
   requested value, the default value will be returned in its place. This method is blocking, it will wait until the
   allocation has been received.*
   
### Value Subscription

You may want to use a value from your allocation without blocking the execution of your application. If this is true, you can
subscribe to a value and apply any actions as a result of it asynchronously.

1. Subscribe to a value from Ascend.
    ```java
        client.subscribe(<key_for_value>, <default_value>, value -> {
            Your code...
        });
    ```
    
    *Note: The return value's type is decided by the provided default value's type. If there is an issue retrieving the
    requested value, the default value will be returned in its place. If you have a previous allocation stored the 
    value will be retrieved and then your code will be executed. When the new allocation is retrieved if the value
    differs from the previously stored allocation then your code will be ran again with the new value. If your code 
    results in an Exception it will be thrown.*
    
### Custom Events (optional)

Sometimes you may want to record certain events that occurred during the participant's session. An example of an event
thats important to record is a "conversion" event. If you implemented the SDK in a shopping app, you could send the
"conversion" event when the participant presses the checkout button.

1. Emit a custom event.
    ```java
       client.emitEvent(<event_type>);
    ```
    
    AND / OR

2. Emit a custom event with an associated score.
    ```java
       client.emitEvent(<event_type>, <score>);
    ```
    
### Contaminate the Allocation (optional)

Sometimes it may be necessary to contaminate the participant's allocation. Meaning, that you may not want that participant's session to be recorded into the experiment.

1. Contaminate the participant's allocation.
    ```java
       client.contaminate();
    ```    
    
### Custom Allocation Store (optional)

Once a participant has been allocated into an experiment you may want to retain the allocations they received. To do this, create a custom allocation store by implementing the AscendAllocationStore interface. You can supply the
custom allocation store to the client when you build the AscendConfig.

1. Supply the allocation store to the client.
    ```java
       AscendConfig config = AscendConfig.Builder(<environment_id>)
           .setAscendAllocationStore(<custom_store>)
           .build();
       AscendClient client = AscendClient.init(config);
   ```

   
### Optional Configurations

There are several optional configurations available through the AscendConfig builder, check out the AscendConfig
documentation to see what options are available.

### Get the .jar

This SDK can also be used in your Java application. To retrieve the .jar:

1. Run the "createJar" gradle command in the top level directory.
    ```
    ./gradlew createJar
    ```
    
2. Retrieve the jar from ascend-sdk/build/outputs/ascend-sdk.jar and place it in your
   project.

### About Evolv and the Ascend Product

Evolv Delivers Autonomous Optimization Across Web & Mobile.

You can find out more by visiting: https://www.evolv.ai/
