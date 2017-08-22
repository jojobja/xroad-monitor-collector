/**
 * The MIT License
 * Copyright (c) 2017, Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.vrk.xroad.monitor.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Default implementation for {@link EnvMonitorDataStorageDao} interface
 * Loads and saves data to Elasticsearch
 */
@Slf4j
@Repository
public class EnvMonitorDataStorageDaoImpl implements EnvMonitorDataStorageDao {

  @Autowired
  private Environment environment;

  private TransportClient client;

  /**
   * Initializes transport client
   * @throws UnknownHostException
   */
  @PostConstruct
  public void init() throws UnknownHostException {
    Settings settings = Settings.builder()
        .put("cluster.name", environment.getProperty("xroad-monitor-collector-elasticsearch.cluster")).build();
    client = new PreBuiltTransportClient(settings)
        .addTransportAddress(new InetSocketTransportAddress(
            InetAddress.getByName(environment.getProperty("xroad-monitor-collector-elasticsearch.host")),
            Integer.parseInt(environment.getProperty("xroad-monitor-collector-elasticsearch.port"))));
  }

  /**
   * Save json data
   * @param index
   * @param type
   * @param json
   * @return
   */
  @Override
  public IndexResponse save(String index, String type, String json) {
    log.info("Elasticsearch data: {}", json);
    return client.prepareIndex(index, type).setSource(json, XContentType.JSON).get();
  }

  /**
   * Load json data
   * @param index
   * @param type
   * @param json
   * @return
   */
  @Override
  public GetResponse load(String index, String type, String json) {
    return client.prepareGet(index, type, json).get();
  }

  /**
   * Closes transport client
   */
  @PreDestroy
  public void shutdown() {
    client.close();
  }
}
