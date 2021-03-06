package com.github.kagkarlsson.scheduler.task;

import com.github.kagkarlsson.scheduler.task.schedule.Daily;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import com.github.kagkarlsson.scheduler.task.schedule.Schedule;
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SchedulesTest {
	private static final Instant NOON_TODAY = ZonedDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0).toInstant();

	@Test
	public void should_validate_pattern() {
		assertIllegalArgument(null);
		assertIllegalArgument("");
		assertIllegalArgument("LALA|123s");

		assertIllegalArgument("DAILY|");
		assertIllegalArgument("DAILY|1200");
		assertIllegalArgument("DAILY|12:00;13:00");
		assertIllegalArgument("DAILY|12:00,13:00,");

		assertParsable("DAILY|12:00", Daily.class);
		Schedule dailySchedule = assertParsable("DAILY|12:00,13:00", Daily.class);
		assertThat(dailySchedule.getNextExecutionTime(complete(NOON_TODAY)), is(NOON_TODAY.plus(Duration.ofHours(1))));

		assertIllegalArgument("FIXED_DELAY|");
		assertIllegalArgument("FIXED_DELAY|123");

		Schedule fixedDelaySchedule = assertParsable("FIXED_DELAY|10s", FixedDelay.class);
		assertThat(fixedDelaySchedule.getNextExecutionTime(complete(NOON_TODAY)), is(NOON_TODAY.plusSeconds(10)));
	}

	private ExecutionComplete complete(Instant timeDone) {
		return ExecutionComplete.success(null, timeDone);
	}

	@SuppressWarnings("rawtypes")
	private Schedule assertParsable(String schedule, Class clazz) {
		Schedule parsed = Schedules.parseSchedule(schedule);
		assertThat(parsed, instanceOf(clazz));
		return parsed;
	}

	private void assertIllegalArgument(String schedule) {
		try {
			Schedules.parseSchedule(schedule);
			fail("Should have thrown IllegalArgument for schedule '" + schedule + "'");
		} catch (Schedules.UnrecognizableSchedule e) {
			assertThat(e.getMessage(), CoreMatchers.containsString("Unrecognized schedule"));
		}
	}
}