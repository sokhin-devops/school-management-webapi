package com.school_management_webapi.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.school_management_webapi.entity.AuditAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Keeps the database's list of allowed audit actions in step with the code.
 *
 * Hibernate writes a CHECK constraint naming every value of an enum column when
 * it creates the table, and ddl-auto=update never revisits it. The audit log is
 * the one enum that keeps growing - each feature adds what it records - and a
 * value missing from the constraint is refused by the database; because the
 * audit write is deliberately allowed to fail quietly, the entry would simply
 * be lost. Rebuilt on every start, it always matches AuditAction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnumConstraintSync implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		String allowed = Arrays.stream(AuditAction.values())
				.map(value -> "'" + value.name() + "'")
				.collect(Collectors.joining(", "));
		try {
			jdbcTemplate.execute("ALTER TABLE audit_events DROP CONSTRAINT IF EXISTS audit_events_action_check");
			jdbcTemplate.execute("ALTER TABLE audit_events ADD CONSTRAINT audit_events_action_check CHECK (action IN ("
					+ allowed + "))");
		} catch (RuntimeException ex) {
			log.warn("Could not refresh the audit action constraint: {}", ex.getMessage());
		}
	}
}
