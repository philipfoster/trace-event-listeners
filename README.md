#RHPAM Process Trace Listeners

The Process Trace framework provides a generic way to asynchronously listen into the execution of a 
RHPAM process instance. Events are published to an AMQ queue for consumption.

###Prerequisites
- Red Hat Process Automation Manager (PAM) 7
- Red Hat AMQ 7
- An existing PAM workflow

---
### Building

To build this repo, simply clone the project and run `mvn clean install`. 
For this to succeed, your maven installation will need to be configured to pull from 
the [Redhat-GA maven repository](https://access.redhat.com/maven-repository), either directly or through a mirror.

---
### Setup 
1. Open PAM standalone-full.xml and make the following updates:

    a. Add the following to the messaging-activemq subsystem section:
    ```
    <pooled-connection-factory   
        name="remote-artemis" 
        entries="java:/RemoteJmsXA java:jboss/RemoteJmsXA"  
        connectors="messaging-remote-broker01-connector" />
    
    <remote-connector name="messaging-remote-broker01-connector"            
              socket-binding="messaging-remote-broker01"/>
    ```
    
    b. Add the following to the `<socket-binding-group>` section, and adjust remote-destination host and port to match your environment:
    ```
    <outbound-socket-binding name="messaging-remote-broker01">
                <remote-destination host="localhost" port="61616"/>
    </outbound-socket-binding>
    ```
   
   c. Go to `domain:naming` subsystem and add the following. This will add the queue. Adjust provider URL and queue names to match your environment
    ````
    <bindings>
        <external-context name="java:global/remoteContext"           
                                    module="org.apache.activemq.artemis" 
                                        class="javax.naming.InitialContext">
                       <environment>
                           <property name="java.naming.factory.initial"     
                             value="org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"/>
                           <property name="java.naming.provider.url" value="tcp://127.0.0.1:61616"/>
                           <property name="queue.TestQueue" value="testQueue"/>
                      </environment>
        </external-context>
      <lookup name="java:/queue/testQueue" 
                    lookup="java:global/remoteContext/TestQueue"/>
    </bindings>
    <remote-naming/>
    ````
2. Ensure that AMQ is running, and restart PAM


---
### Attaching the Listeners
First, ensure that you have built this project as described above, and the built JAR is in a maven 
repository that PAM is configured to pull from (Your local maven cache is ok if testing locally).

Open the PAM project in Business Central. Click Settings -> Dependencies -> Add from . Type the following into the blank row that appeared:

| Group ID	| Artifact ID 	| Version |
|---	    |---	        |---	  |
|com.malware  |trace-event-listeners  | 1.0-SNAPSHOT |

Click Deployments -> Event Listeners -> Add event listener
In the blank row that appeared, type `com.malware.tracelisteners.ProcessTraceEventListener`, and select "Reflection" from the dropdown box.
Click Save, and type an appropriate commit message. 

The event listeners are now attached to your project, and will begin running on your next deployment. 

---
### Message Formatting
An event will be published for each process instance when it starts as well as when it is completed.
Additionally, an event will be published each time a node in the process instance is started and completed. 

The messages will follow this general format:
```
{ 
  "ProcessInstanceId":"18",
  "TimeStamp":"2020-06-19T15:42:34.131",
  "EventType":"ProcessTraceEvent",
  "Type":"BeforeNodeTriggered",
  "Process":{
    "Name":"testprocess",
    "Node":{
      "StartedOn":"2020-06-19T15:42:34.131",
      "CompletedOn":null,
      "State":"Completed",
      "Name":"end"
    }
  }
}
```

 