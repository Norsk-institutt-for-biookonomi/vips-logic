# Troubleshooting
Various tips and tricks for fixing problems are added here

## [2021] Transaction timeouts
If you get weird errors when running jobs, e.g. like this:

```
2021-01-05 18:34:48,916 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::chec
k timeout for TX 0:ffff8027c039:65d436c5:5ff4a101:6f in state  RUN
2021-01-05 18:34:48,925 WARN  [com.arjuna.ats.arjuna] (Transaction Reaper Worker 0) ARJUNA012095: Abort of actio
n id 0:ffff8027c039:65d436c5:5ff4a101:6f invoked while multiple threads active within it.
[...]
```

...this may be because the JEE application server has a transaction timeout. In WildFly (at least in versions >= 16), the default timeout is 300 seconds. This can be adjusted, e.g. in standalone.xml. In the transaction subsystem add the `default-timeout="900"` attribute inside the `<coordinator-environment>` element below. 900 seconds equals 15 minutes.

```xml
<subsystem xmlns="urn:jboss:domain:transactions:5.0">
            <core-environment node-identifier="${jboss.tx.node.id:1}">
                <process-id>
                    <uuid/>
                </process-id>
            </core-environment>
            <recovery-environment socket-binding="txn-recovery-environment" status-socket-binding="txn-status-manager"/>
            <coordinator-environment default-timeout="1000" statistics-enabled="${wildfly.transactions.statistics-enabled:${wildfly.statistics-enabled:false}}"/>
            <object-store path="tx-object-store" relative-to="jboss.server.data.dir"/>
        </subsystem>
```