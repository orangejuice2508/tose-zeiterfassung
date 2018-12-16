package de.gruppe2.agamoTTTo.service;

import de.gruppe2.agamoTTTo.domain.base.PoolDateFilter;
import de.gruppe2.agamoTTTo.domain.entity.Record;
import de.gruppe2.agamoTTTo.domain.entity.RecordLog;
import de.gruppe2.agamoTTTo.domain.entity.User;
import de.gruppe2.agamoTTTo.repository.RecordLogRepository;
import de.gruppe2.agamoTTTo.repository.RecordRepository;
import de.gruppe2.agamoTTTo.domain.base.ChangeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static java.time.Duration.between;

@Service
public class RecordService{

    private RecordRepository recordRepository;

    private RecordLogRepository recordLogRepository;

    @Autowired
    public RecordService(RecordRepository recordRepository, RecordLogRepository recordLogRepository) {
        this.recordRepository = recordRepository;
        this.recordLogRepository = recordLogRepository;
    }

    /**
     * This method uses the recordRepository to try to add a record to the database.
     * Furthermore the new record is logged in the database.
     *
     * @param record the record as obtained from the controller
     */
    public void addRecord(Record record) {

        record.setDuration(calculateDuration(record.getStartTime(), record.getEndTime()));
        recordRepository.save(record);
        recordLogRepository.save(new RecordLog(record, ChangeType.created));
    }

    public List<Record> getAllRecordsByParameters (PoolDateFilter filter, User authenticationUser){
        // If no date is set, set it to a default date. Reason: Date is optional in the filter.
        LocalDate from = filter.getFrom() != null ? filter.getFrom() : LocalDate.of(1000,1,1);
        LocalDate to = filter.getTo() != null ? filter.getTo() : LocalDate.of(9999,12,31);

        return recordRepository.findAllByUserAndPoolAndDateBetweenOrderByDateAscStartTimeAsc(authenticationUser, filter.getPool(), from, to);
    }

    /**
     * This method checks if a record's times are valid.
     *
     * @param record the new record
     * @return true if endTime is after startTime
     */
    public boolean areTimesValid(Record record) {
        return record.getEndTime().isAfter(record.getStartTime());
    }

    /**
     * This method checks if a record's time are allowed, i.e. that the times of the new record
     * do NOT overlap with an already existing record.
     *
     * @param newRecord the record which should be saved
     * @param user the user who wants to add a record
     * @return true if the times are allowed (i.e. do NOT overlap); false if the time are not allowed
     */
    public boolean areTimesAllowed(Record newRecord, User user) {

        LocalTime newStartTime = newRecord.getStartTime();
        LocalTime newEndTime = newRecord.getEndTime();
        LocalDate newDate = newRecord.getDate();

        // Get a user's current records of the specific day when he wants to add his new record
        Set<Record> currentUserRecords = recordRepository.findAllByUserAndDate(user, newDate);

        if (!currentUserRecords.isEmpty()) {
            for (Record currentRecord : currentUserRecords) {
                LocalTime currentStartTime = currentRecord.getStartTime();
                LocalTime currentEndTime = currentRecord.getEndTime();

                // Check if the new record starts between the times of a current record.
                if (newStartTime.isBefore(currentEndTime) && newStartTime.isAfter(currentStartTime)) {
                    return false;
                }

                // Check if the new record ends between the times of a current record.
                if (newEndTime.isAfter(currentStartTime) && newEndTime.isBefore(currentEndTime)) {
                    return false;
                }

                // Check if the new record lies completely between the times of a current record.
                if (newStartTime.isAfter(currentStartTime) && newEndTime.isBefore(currentEndTime)) {
                    return false;
                }

                // Check if the new record encompasses the times of a current record.
                if (newStartTime.isBefore(currentStartTime) && newEndTime.isAfter(currentEndTime)) {
                    return false;
                }

                // Check if the new record starts or and at the start or end of a current record.
                if (newStartTime.equals(currentStartTime) || newEndTime.equals(currentEndTime)) {
                    return false;
                }
            }
        }

        // If the user doesn't have any records on that day, the times are allowed.
        return true;
    }

    /**
     * This method calculates the duration of time a user worked.
     *
     * @param startTime The start time of the task
     * @param endTime the end time of the task
     * @return The duration as LocalTime
     */
    private LocalTime calculateDuration(LocalTime startTime, LocalTime endTime) {

        Duration duration = between(startTime, endTime);
        int hours = (int) duration.toHours();
        int minutes = (int) duration.toMinutes() - (60 * hours);

        return LocalTime.of(hours,minutes);
    }
}
