_systemd_
=========

Making a service
----------------

We want the bridge to be running at all times. To achieve this we can create a
_systemd_ unit that will start the bridge service on boot and maintain it,
restarting the process after failure.

This will also concatenate any `stdout` logging the bridge does into `syslog`,
allowing services such as _Papertrail_ to pick up on the bridge's logs.

### Creating a _systemd_ unit

Unit files for _systemd_ are stored in `/etc/systemd/system`. Let's create one:

```sh
unifi-id@unifi-services:~$ sudo -e "/etc/systemd/system/cw-capsule-bridge.service"
```

Configure the file as below:

```ini
[Unit]
Description=Central Working Capsule-unifi.id bridge

[Service]
Type=idle
WorkingDirectory=/home/unifi-id/unifi.id/cw-capsule-bridge
ExecStart=/usr/bin/npm run start
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

#### Comments

* `Type=idle`
    * Makes this service run after other units are finished initialising.
* `WorkingDirectory`
    * This needs to be set to the root of the capsule bridge installation.
    * The home directory used here will need to be changed if the default user
      is not `unifi-id` - for example, in the Vagrant testing environment it
      will be `/home/vagrant` instead.
* `Restart=always`
    * Makes this service always restart after any failure state.
* `RestartSec`
    * How long to wait before restarting the service as above.
* `WantedBy=multi-user.target`
    * Defines at what level of system functioning this unit will be required.
    * `multi-user.target` is the stage in which a user can login to the system,
      but before a graphical environment is loaded.

### Enabling the _systemd_ unit

Now the service unit has been created, we can enable it:

```sh
# Reload systemd unit files
unifi-id@unifi-services:~$ sudo systemctl daemon-reload

# Enable the service to run on boot and start it immediately
unifi-id@unifi-services:~$ sudo systemctl enable cw-capsule-bridge
unifi-id@unifi-services:~$ sudo systemctl start cw-capsule-bridge
```

The service is now running and managed by _systemd_. It will be started as part
of the boot process, and restarted if it ever fails for any reason. As a bonus
_systemd_ will automatically push any `stdout` messages to the system journal.
