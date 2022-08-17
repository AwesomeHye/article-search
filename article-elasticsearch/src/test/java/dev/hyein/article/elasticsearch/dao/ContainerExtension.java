package dev.hyein.article.elasticsearch.dao;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import dev.hyein.article.elasticsearch.properties.ArticleProperties;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ContainerExtension implements BeforeAllCallback, AfterAllCallback {
    public static final int START_CONTAINER_WAIT_MS = 20000;

    static GenericContainer<?> esContainer;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // get beans
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        ArticleProperties articleProperties = applicationContext.getBean(ArticleProperties.class);
        ArticleDao articleDao = applicationContext.getBean(ArticleDao.class);

        // create container
        startContainer(articleProperties.getPort());
        Thread.sleep(START_CONTAINER_WAIT_MS); // ES 띄워질 때까지 대기

        // create index
        String mappings = new String(Files.readAllBytes(Paths.get("mappings.txt")));
        articleDao.createIndex(mappings);
    }

    private static void startContainer(int port) {
        esContainer = new GenericContainer<>(new ImageFromDockerfile().withDockerfileFromBuilder(
                builder -> builder.from("docker.elastic.co/elasticsearch/elasticsearch:7.8.1")
                        .run("bin/elasticsearch-plugin", "install", "analysis-nori")
                        .build()
        ));

        esContainer
                .withEnv("discovery.type", "single-node")
                .withEnv("http.host", "0.0.0.0")
                .withCreateContainerCmdModifier(command -> {
                            HostConfig hostConfig = new HostConfig();
                            hostConfig.withPortBindings(new PortBinding(Ports.Binding.bindPort(port), new ExposedPort(9200)));
                            command.withHostConfig(hostConfig);
                        });
        esContainer.start();

        String host = String.format("http://%s:%s", esContainer.getContainerIpAddress(), esContainer.getMappedPort(9200));
        System.out.println("host: " + host);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        esContainer.close();
    }
}
