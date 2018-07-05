package org.graylog2.plugins.teams;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugins.teams.configuration.TeamsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class TeamsClient {

    private static final Logger LOG = LoggerFactory.getLogger(TeamsClient.class);

    private final String webhookUrl;
    private final String proxyURL;

    public TeamsClient(Configuration configuration) {
        this.webhookUrl = configuration.getString(TeamsConfiguration.CK_WEBHOOK_URL);
        this.proxyURL = configuration.getString(TeamsConfiguration.CK_PROXY_ADDRESS);
    }

    public void send(TeamsMessage message) throws TeamsClientException {
        final URL url;
        try {
            url = new URL(webhookUrl);
        } catch (MalformedURLException e) {
            throw new TeamsClientException("Error while constructing webhook URL.", e);
        }

        final HttpURLConnection conn;
        try {
            if (!StringUtils.isEmpty(proxyURL)) {
                final URI proxyUri = new URI(proxyURL);
                InetSocketAddress sockAddress = new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort());
                final Proxy proxy = new Proxy(Proxy.Type.HTTP, sockAddress);
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
        } catch (URISyntaxException | IOException e) {
            throw new TeamsClientException("Could not open connection to Teams API", e);
        }

        try (final Writer writer = new OutputStreamWriter(conn.getOutputStream())) {
            String json = message.getJsonString();
            writer.write(json);
            writer.flush();

            final int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                if(LOG.isDebugEnabled()){
                    try (final InputStream responseStream = conn.getInputStream()) {
                        final byte[] responseBytes = ByteStreams.toByteArray(responseStream);
                        final String response = new String(responseBytes, Charsets.UTF_8);
                        LOG.debug("Received HTTP response body:\n{}", response);
                    }
                }
                throw new TeamsClientException("Unexpected HTTP response status " + responseCode);
            }
        } catch (IOException e) {
            throw new TeamsClientException("Could not POST to Teams API", e);
        }

        try (final InputStream responseStream = conn.getInputStream()) {
            final byte[] responseBytes = ByteStreams.toByteArray(responseStream);

            final String response = new String(responseBytes, Charsets.UTF_8);
            if (response.equals("ok")) {
                LOG.debug("Successfully sent message to Teams.");
            } else {
                LOG.warn("Message couldn't be successfully sent. Response was: {}", response);
            }
        } catch (IOException e) {
            throw new TeamsClientException("Could not read response body from Teams API", e);
        }
    }


    public class TeamsClientException extends Exception {

        public TeamsClientException(String msg) {
            super(msg);
        }

        public TeamsClientException(String msg, Throwable cause) {
            super(msg, cause);
        }

    }

}
