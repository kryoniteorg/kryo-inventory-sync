package org.kryonite.kryoplayersync.paper.persistence.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kryonite.kryoplayersync.paper.persistence.impl.MariaDbInventoryRepository.CREATE_INVENTORY_TABLE;
import static org.kryonite.kryoplayersync.paper.persistence.impl.MariaDbInventoryRepository.GET_INVENTORY;
import static org.kryonite.kryoplayersync.paper.persistence.impl.MariaDbInventoryRepository.INSERT_INVENTORY;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MariaDbInventoryRepositoryTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HikariDataSource dataSourceMock;

  @Test
  void shouldCreateTableOnStartup() throws SQLException {
    // Arrange - Act
    new MariaDbInventoryRepository(dataSourceMock);

    // Assert
    verify(dataSourceMock.getConnection()).prepareStatement(CREATE_INVENTORY_TABLE);
  }

  @Test
  void shouldSaveInventory() throws SQLException {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    byte[] inventory = new byte[] {12, 13};

    MariaDbInventoryRepository testee = new MariaDbInventoryRepository(dataSourceMock);

    // Act
    testee.save(uniqueId, inventory);

    // Assert
    verify(dataSourceMock.getConnection()).prepareStatement(INSERT_INVENTORY);
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_INVENTORY)).setString(1, uniqueId.toString());
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_INVENTORY)).setBytes(2, inventory);
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_INVENTORY)).setBytes(3, inventory);

    verify(dataSourceMock.getConnection().prepareStatement(INSERT_INVENTORY)).executeUpdate();
  }

  @Test
  void shouldReturnInventory() throws SQLException {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    byte[] inventory = new byte[] {12, 13};

    MariaDbInventoryRepository testee = new MariaDbInventoryRepository(dataSourceMock);
    when(dataSourceMock.getConnection().prepareStatement(GET_INVENTORY).executeQuery().first()).thenReturn(true);
    when(dataSourceMock.getConnection().prepareStatement(GET_INVENTORY).executeQuery().getBytes("data"))
        .thenReturn(inventory);

    // Act
    Optional<byte[]> result = testee.get(uniqueId);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(inventory, result.get());
    verify(dataSourceMock.getConnection(), atLeastOnce()).prepareStatement(GET_INVENTORY);
    verify(dataSourceMock.getConnection().prepareStatement(GET_INVENTORY)).setString(1, uniqueId.toString());
  }
}
