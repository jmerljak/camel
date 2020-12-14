/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.salesforce;

import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.Connection;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.http.HttpChannelOverHTTP;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.client.http.HttpConnectionOverHTTP;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.Promise;

/**
 * A workaround for Fiddler sending <code>Connection: close</code> on HTTPS proxy tunneling via CONNECT.
 */
public class SalesforceHttpClientTransportOverHTTP extends HttpClientTransportOverHTTP {

    public class SalesforceHttpChannelOverHTTP extends HttpChannelOverHTTP {

        public SalesforceHttpChannelOverHTTP(final HttpConnectionOverHTTP connection) {
            super(connection);
        }

        @Override
        public void exchangeTerminated(final HttpExchange exchange, final Result result) {
            final Response response = result.getResponse();
            final HttpRequest request = exchange.getRequest();

            if (response != null && response.getVersion() != null && response.getVersion().compareTo(HttpVersion.HTTP_1_1) >= 0
                    && request != null && HttpMethod.CONNECT.is(request.getMethod())) {
                final HttpFields headers = response.getHeaders();
                headers.remove(HttpHeader.CONNECTION);
            }

            super.exchangeTerminated(exchange, result);
        }
    }

    public class SalesforceHttpConnectionOverHTTP extends HttpConnectionOverHTTP {

        public SalesforceHttpConnectionOverHTTP(final EndPoint endPoint, final HttpDestination destination,
                                                final Promise<Connection> promise) {
            super(endPoint, destination, promise);
        }

        @Override
        protected HttpChannelOverHTTP newHttpChannel() {
            return new SalesforceHttpChannelOverHTTP(this);
        }
    }

    @Override
    protected HttpConnectionOverHTTP newHttpConnection(
            final EndPoint endPoint, final HttpDestination destination, final Promise<Connection> promise) {
        return new SalesforceHttpConnectionOverHTTP(endPoint, destination, promise);
    }
}
