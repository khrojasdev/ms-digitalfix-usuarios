package com.digitalfix.msusuarios.service;

import com.digitalfix.msusuarios.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AppUserServiceTest {

    @Autowired
    private AppUserService appUserService;
    @Autowired
    private com.digitalfix.msusuarios.repository.AppUserRepository appUserRepository;

    @Test
    public void testAutoProvisionConcurrency() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successfulReturns = new AtomicInteger(0);

        String testOid = "oid-concurrencia-123";

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    // 10 hilos intentan registrar al mismo usuario exactamente al mismo tiempo
                    AppUser user = appUserService.autoProvision(testOid, "test@empresa.com", "Usuario Test");
                    if (user != null && user.getId() != null) {
                        successfulReturns.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Esperar a que terminen los 10 hilos

        // Si la idempotencia y la BD funcionan, las 10 llamadas debieron devolver el mismo usuario sin crashear
        assertEquals(10, successfulReturns.get());
    }
    @Test
    public void givenInactiveUser_whenAutoProvision_thenThrowsException() {
        // 1. Crear usuario y guardarlo activo
        String testOid = "oid-bloqueo-789";
        appUserService.autoProvision(testOid, "bloqueado@test.com", "Usuario a Bloquear");

        // 2. Buscarlo en BD y cambiar su estado a inactivo (simulando que un admin lo bloqueó)
        AppUser userToDeactivate = appUserRepository.findByAzureOid(testOid).get();
        userToDeactivate.setActive(false);
        appUserRepository.save(userToDeactivate);

        // 3. Intentar hacer autoprovisión de nuevo y verificar que lance la excepción
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            appUserService.autoProvision(testOid, "bloqueado@test.com", "Usuario a Bloquear");
        });
    }
}