AppOptics
=========

We use the _AppOptics_ web service to monitor our deployed agents. Each agent
needs to be configured to pass information over an HTTP connection to the
_AppOptics_ service.

In this case we want process information for our bridge to appear in the
_AppOptics_ dashboard. We can achieve this by configuring the _collectd_ daemon
to monitor the Node process that runs our bridge.

_collectd_
----------

First, install `collectd` if it isn't already:

```sh
unifi-id@unifi-services:~$ sudo apt install collectd
```

Next, we need to configure _collectd_. The main configuration is stored in
`/etc/collectd/collectd.conf`. Let's open it for editing:

```sh
unifi-id@unifi-services:~$ sudo -e "/etc/collectd/collectd.conf"
```

You can read more about the format of this file in [the official _collectd_
documentation](https://collectd.org/documentation.shtml).

We now need to configure and enable 2 _collectd_ plugins for the _AppOptics_
integration to work.

### `write_http` Plugin

We can configure this plugin to send logs over HTTP to an arbitrary URI. We will use this to forward our logs to AppOptics.

#### Enable:
```
LoadPlugin write_http
```

#### Configure:
```
<Plugin "write_http">
  <Node "AppOptics">
    URL "https://collectd.librato.com/v1/measurements"
    User "ops@unifi.id"
    Password "a02222ad2063829f3eb28e660e27e28847f748b77c1903ff1206260a2ba29911"
    Format "JSON"
  </Node>
</Plugin>
```

### `processes` Plugin

This plugin will log information about running processes if configured correctly. Let's set it to monitor the `node` process, which runs our Capsule bridge.

#### Enable:
```
LoadPlugin processes
```

#### Configure:
```
<Plugin "processes">
	CollectFileDescriptor "false"
	CollectContextSwitch "false"
	<Process "node">
		CollectFileDescriptor "true"
		CollectContextSwitch "true"
	</Process>
</Plugin>
```
