package com.notpatch.hazeCore.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class DatabaseConfiguration extends OkaeriConfig {

    @Comment("Database Settings")
    private Database database = new Database();

    @Getter
    @Setter
    public static class Database extends OkaeriConfig {

        @Comment("Database type: mysql, sqlite")
        private String type = "sqlite";

        @Comment("Database host")
        private String host = "localhost";

        @Comment("Database port")
        private int port = 3306;

        @Comment("Database name")
        private String database = "hazecore";

        @Comment("Database username")
        private String username = "root";

        @Comment("Database password")
        private String password = "<PASSWORD>";

        @Comment("Database pool size")
        private int poolSize = 10;

        @Comment("Database pool minimum idle")
        private int minimumIdle = 5;

        @Comment("Database pool maximum lifetime in seconds")
        private int maximumLifetime = 1800000;

        @Comment("Database keep alive time in seconds")
        private int keepaliveTime = 0;

        @Comment("Database connection timeout in seconds")
        private int connectionTimeout = 30000;

    }

}
