package com.flowcapture.mediaagent.repository;

import com.flowcapture.mediaagent.exception.AssetIngestionException;
import com.flowcapture.mediaagent.model.MediaAsset;
import com.flowcapture.mediaagent.model.VideoAsset;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * AssetDaoImpl is a genuine JDBC-backed persistence implementation using an
 * embedded SQLite database (via the org.xerial:sqlite-jdbc driver). It
 * models exactly what a real DAO does against any relational database:
 * open a connection, execute parameterized SQL, and translate low-level
 * SQLException failures into the domain-specific AssetIngestionException.
 *
 * The database file path is injectable through the constructor, which is
 * what makes this class independently unit-testable against a disposable
 * temporary database file rather than the application's real one.
 */
public class AssetDaoImpl implements AssetDao {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS assets (" +
                    "asset_id TEXT PRIMARY KEY," +
                    "title TEXT NOT NULL," +
                    "status TEXT NOT NULL," +
                    "asset_type TEXT NOT NULL," +
                    "resolution TEXT," +
                    "frame_rate REAL," +
                    "codec TEXT" +
                    ")";

    private static final String UPSERT_SQL =
            "INSERT INTO assets (asset_id, title, status, asset_type, resolution, frame_rate, codec) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(asset_id) DO UPDATE SET " +
                    "title = excluded.title, " +
                    "status = excluded.status, " +
                    "asset_type = excluded.asset_type, " +
                    "resolution = excluded.resolution, " +
                    "frame_rate = excluded.frame_rate, " +
                    "codec = excluded.codec";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM assets WHERE asset_id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM assets ORDER BY asset_id";

    private final String jdbcUrl;

    /**
     * Default constructor: persists to media_agent.db in the current
     * working directory, which is what Main uses in normal operation.
     */
    public AssetDaoImpl() throws AssetIngestionException {
        this("media_agent.db");
    }

    /**
     * Constructor that accepts an explicit database file path, allowing
     * tests to point this DAO at a disposable temporary file instead of
     * the application's real database.
     */
    public AssetDaoImpl(String databaseFilePath) throws AssetIngestionException {
        this.jdbcUrl = "jdbc:sqlite:" + databaseFilePath;
        initializeSchema();
    }

    private void initializeSchema() throws AssetIngestionException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new AssetIngestionException("Failed to initialize database schema at " + jdbcUrl, e);
        }
    }

    @Override
    public void saveAsset(MediaAsset asset) throws AssetIngestionException {
        String resolution = null;
        Double frameRate = null;
        String codec = null;

        if (asset instanceof VideoAsset) {
            VideoAsset videoAsset = (VideoAsset) asset;
            resolution = videoAsset.getResolution();
            frameRate = videoAsset.getFrameRate();
            codec = videoAsset.getCodec();
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {

            statement.setString(1, asset.getAssetId());
            statement.setString(2, asset.getTitle());
            statement.setString(3, asset.getStatus());
            statement.setString(4, asset.getAssetType());
            statement.setString(5, resolution);
            if (frameRate != null) {
                statement.setDouble(6, frameRate);
            } else {
                statement.setNull(6, Types.REAL);
            }
            statement.setString(7, codec);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new AssetIngestionException(
                    "Failed to persist asset with id '" + asset.getAssetId() + "' to " + jdbcUrl, e);
        }
    }

    @Override
    public MediaAsset findById(String assetId) throws AssetIngestionException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            statement.setString(1, assetId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToAsset(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new AssetIngestionException("Failed to read asset with id '" + assetId + "' from " + jdbcUrl, e);
        }
    }

    @Override
    public List<MediaAsset> findAll() throws AssetIngestionException {
        List<MediaAsset> assets = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL)) {

            while (resultSet.next()) {
                assets.add(mapRowToAsset(resultSet));
            }
            return assets;
        } catch (SQLException e) {
            throw new AssetIngestionException("Failed to list assets from " + jdbcUrl, e);
        }
    }

    private MediaAsset mapRowToAsset(ResultSet resultSet) throws SQLException {
        String assetId = resultSet.getString("asset_id");
        String title = resultSet.getString("title");
        String status = resultSet.getString("status");
        String assetType = resultSet.getString("asset_type");
        String resolution = resultSet.getString("resolution");
        double frameRate = resultSet.getDouble("frame_rate");
        String codec = resultSet.getString("codec");

        if ("VIDEO".equals(assetType)) {
            return new VideoAsset(assetId, title, status, resolution, frameRate, codec);
        }
        return null;
    }
}
