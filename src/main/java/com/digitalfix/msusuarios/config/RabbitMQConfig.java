package com.digitalfix.msusuarios.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia de mensajeria del microservicio de usuarios.
 *
 * usuarios es un PRODUCTOR: publica eventos cuando se crea o cambia el estado
 * de una cuenta. Usa un exchange de tipo topic para que distintos consumidores
 * (por ejemplo auditoria) puedan suscribirse por patron de routing key.
 *
 * El nombre del exchange se lee desde application.yml bajo amqp.*, para no
 * escribirlo a mano en el codigo.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${amqp.exchange.usuarios}")
    private String usuariosExchange;

    @Bean
    public TopicExchange usuariosExchange() {
        return new TopicExchange(usuariosExchange);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
