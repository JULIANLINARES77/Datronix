package uniminuto.datronix.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import uniminuto.datronix.entity.EmailVerificationCode;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findFirstByUsuario_IdUsuarioAndPurposeOrderByCreatedAtDesc(
            String idUsuario, String purpose);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select code from EmailVerificationCode code
            where code.usuario.idUsuario = :idUsuario
              and code.purpose = :purpose
            order by code.createdAt desc
            """)
    Optional<EmailVerificationCode> findLatestForUpdate(
            @Param("idUsuario") String idUsuario,
            @Param("purpose") String purpose);

    void deleteAllByUsuario_IdUsuarioAndPurpose(String idUsuario, String purpose);

    void deleteAllByUsuario_IdUsuario(String idUsuario);
}
