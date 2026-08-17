/*
 * Copyright (c) 2026 NIBIO <http://www.nibio.no/>.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package no.nibio.vips.logic.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import no.nibio.vips.logic.util.Globals;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Quarkus config for values previously read from WildFly system properties at class-load time.
 */
@ApplicationScoped
public class VipsLogicConfig {

    private static final Logger LOG = Logger.getLogger(VipsLogicConfig.class);

    @ConfigProperty(name = "vipslogic.protocol")
    String protocol;

    @ConfigProperty(name = "vipslogic.server-name")
    String serverName;

    void onStart(@Observes StartupEvent event) {
        Globals.PROTOCOL = protocol;
        Globals.SERVER_NAME = serverName;
        LOG.infof("VIPSLogic config: protocol=%s serverName=%s", protocol, serverName);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getServerName() {
        return serverName;
    }
}
