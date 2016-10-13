# Folding@home binding

This binding can control multiple Folding@home client instances and
slots, using the TCP interface. It provides control over Run / Pause and Finish.
It polls for the status of the client, updates the run / pause state, and provides 
a basic description of the slot. 

The clients must be added manually in the Paper UI, but the slots for that 
client will then appear using auto-discovery.

## Requirements
The TCP interface is enabled only on localhost by default, without a password.
In order to allow control of Folding on other machines than the one running 
OpenHAB, it is necessary to configure the Folding client to listen on a 
non-localhost address. (TODO: describe)
