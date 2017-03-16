# Multiplay
Multiplayer via wi-fi P2P.

## Usage:<br>
### 1. Add to root build.gradle:
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Add to app/build.gradle:
```gradle
dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile 'com.github.luna-park:Multiplay:1.0'
}
```
### 3. Add to MainActivity.java:
```java
public class MainActivity extends Activity implements MultiplayEvent {
...
   multiplay = new Multiplay(Context, MultiplayEvent, port, buffer_size);
   
   // Start peers discovery
   multiplay.discoverPeers();
....
```
... and implements methods:
#### For any fails:
```java
    void onFailure(String reason);
```
#### Successfully connection and ready for data exchange:
```java
    void onConnectionChange(boolean isClient);
```

#### On receive wi-fi_p2p_ready device list:
```java
    void onChangeDeviceList(ArrayList<WifiP2pDevice> wifiP2pDevices);
```

#### On data receive:
```java
    void onReceiveData(String s);
```

### For data send use:
```java
    multiplay.send("Data");   
```
or
```java
    multiplay.send(new byte[16]);
```
