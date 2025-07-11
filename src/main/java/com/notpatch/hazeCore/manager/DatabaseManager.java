package com.notpatch.hazeCore.manager;

import com.notpatch.hazeCore.HazeCore;
import com.notpatch.hazeCore.model.HazeModule;
import com.notpatch.hazeCore.util.NLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {
    @Getter
    private HikariDataSource dataSource;
    private final ExecutorService executor;
    private final HazeCore main;
    private final HazeModule module;
    @Getter
    private boolean usingSQLite = false;

    public DatabaseManager(HazeModule module) {
        this.main = HazeCore.getInstance();
        this.module = module;
        this.executor = Executors.newFixedThreadPool(10);
    }

    public void connect() {
        if(module.getDatabaseConfig().getDatabase().getType().equalsIgnoreCase("mysql")){
            if(!connectToMySQL()){
                NLogger.warn("Unable to connect to MySQL database, falling back to SQLite.");
            }else{
                connectToMySQL();
            }
        }else{
            connectToSQLite();
        }
    }

    private boolean connectToMySQL() {
        try {
            HikariConfig config = new HikariConfig();

            String host = module.getDatabaseConfig().getDatabase().getHost();
            String database = module.getDatabaseConfig().getDatabase().getDatabase();
            String username = module.getDatabaseConfig().getDatabase().getUsername();
            String password = module.getDatabaseConfig().getDatabase().getPassword();
            int port = module.getDatabaseConfig().getDatabase().getPort();

            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);

            configureMySQLPool(config);

            dataSource = new HikariDataSource(config);
            testConnection();

            NLogger.info("Connected to MySQL | " + module.getName());
            return true;

        } catch (Exception e) {
            return false;
        }
    }


    private void connectToSQLite() {
        try {
            Class.forName("org.sqlite.JDBC");

            File dataFolder = main.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            File dbFile = new File(module.folderPath(), "database.db");
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
            
            configureSQLitePool(config);
            
            dataSource = new HikariDataSource(config);
            usingSQLite = true;
            
            testConnection();
            NLogger.info("Connected to SQLite");

        } catch (Exception e) {
            NLogger.error("Unable to connect to SQLite database! Check your config.yml file for correct connection settings and try again. If the problem persists, contact the developer for support.!");
        }
    }

    private void configureMySQLPool(HikariConfig config) {

        int poolSize = module.getDatabaseConfig().getDatabase().getPoolSize();
        int minimumIdle = module.getDatabaseConfig().getDatabase().getMinimumIdle();
        long maxLifeTime = module.getDatabaseConfig().getDatabase().getMaximumLifetime();
        int keepAliveTime = module.getDatabaseConfig().getDatabase().getKeepaliveTime();
        long connectionTimeout = module.getDatabaseConfig().getDatabase().getConnectionTimeout();

        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(minimumIdle);
        config.setMaxLifetime(maxLifeTime);
        config.setKeepaliveTime(keepAliveTime);
        config.setConnectionTimeout(connectionTimeout);
        
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
    }

    private void configureSQLitePool(HikariConfig config) {
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setPoolName(module.getName().toLowerCase(Locale.ENGLISH));
    }

    //Example
    /*private void initializeSQLiteTables() {

        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(16) NOT NULL,
                level INTEGER DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        executeAsync(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(createTableSQL)) {
                ps.executeUpdate();
                return null;
            }
        }).exceptionally(throwable -> {
            NLogger.error("SQLite tabloları oluşturulurken hata oluştu!");
            NLogger.exception((Exception) throwable);
            return null;
        });
    }*/

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        executor.shutdown();
    }

    private void testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1000)) {
                NLogger.error("Database connection test failed!");
            }
        } catch (SQLException e) {
            NLogger.error("Database connection test failed!");
        }
    }

    public <T> CompletableFuture<T> executeAsync(SqlFunction<Connection, T> function) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                return function.apply(conn);
            } catch (SQLException e) {
                NLogger.error("Database exception:");
                NLogger.exception(e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<Integer> updateAsync(String sql, Object... params) {
        return executeAsync(conn -> {
            try (PreparedStatement ps = prepare(conn, sql, params)) {
                return ps.executeUpdate();
            }
        });
    }

    public <T> CompletableFuture<T> queryAsync(String sql, ResultSetFunction<T> function, Object... params) {
        return executeAsync(conn -> {
            try (PreparedStatement ps = prepare(conn, sql, params);
                 ResultSet rs = ps.executeQuery()) {
                return function.apply(rs);
            }
        });
    }

    public CompletableFuture<int[]> executeBatchAsync(String sql, BatchPreparedStatementSetter setter) {
        return executeAsync(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < setter.getBatchSize(); i++) {
                    setter.setValues(ps, i);
                    ps.addBatch();
                }
                return ps.executeBatch();
            }
        });
    }

    private PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        if (usingSQLite) {
            sql = convertToSQLite(sql);
        }
        
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }

    private String convertToSQLite(String sql) {
        return sql.replaceAll("AUTO_INCREMENT", "AUTOINCREMENT")
                 .replaceAll("CURRENT_TIMESTAMP\\(\\)", "CURRENT_TIMESTAMP");
    }

    @FunctionalInterface
    public interface SqlFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultSetFunction<T> {
        T apply(ResultSet rs) throws SQLException;
    }

    public interface BatchPreparedStatementSetter {
        void setValues(PreparedStatement ps, int i) throws SQLException;
        int getBatchSize();
    }
}