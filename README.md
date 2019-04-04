# Ascend Android SDK

#### Vocabulary

   **Participant:** The end user of the application, the individual who's actions are being recorded in the experiment. 
   **Allocation:** The set of configurations that have been give to the participant, the values that are being
   experimented against.

For a complete example of how to use this SDK see our [example app](https://github.com/evolv-ai/ascend-android-sdk/blob/master/example/src/main/java/ai/evolv/ascend/example/MainActivity.java).

### Client Initialization

1. Build an AscendConfig instance.
    ```java
       AscendConfig config = new AscendConfig.Builder(<environment_id>).build();
    ```

2. Initialize the AscendClient.
    ```java
       AscendClient client = new AscendClient.init(config);
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
   requested value, the default value will be returned in its place.*
    
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

Sometimes it may be necessary to contaminate the participant's allocation. Meaning, that you may not want that
participant's session to be recorded into the experiment.

1. Contaminate the participant's allocation.
    ```java
       client.contaminate();
    ```    
    
### Custom Allocation Store (optional)

Once a participant has been allocated into an experiment you may want to retain the allocations they received. To
do this create a custom allocation store by implementing the AscendAllocationStore interface. You can supply the
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

### About Evolv and the Ascend Product

Evolv Delivers Autonomous Optimization Across Web & Mobile.

You can find out more by visiting: https://www.evolv.ai/
