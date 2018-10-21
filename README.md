# genak

Kotlin 1.3 skeleton project / experimental load tool 


## PREREQUISITES

For udp reporting 
https://docs.influxdata.com/influxdb/v1.6/supported_protocols/udp/

If the values are less than 26214400 bytes (25MB), you should add the following lines to the /etc/sysctl.conf file:
```
net.core.rmem_max=26214400
net.core.rmem_default=26214400
```
Changes to /etc/sysctl.conf do not take effect until reboot. To update the values immediately, type the following commands as root:
```
sysctl -w net.core.rmem_max=26214400
sysctl -w net.core.rmem_default=26214400
```
