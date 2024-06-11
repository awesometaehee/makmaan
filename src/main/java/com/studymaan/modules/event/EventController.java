package com.studymaan.modules.event;

import com.studymaan.modules.account.CurrentAccount;
import com.studymaan.modules.account.Account;
import com.studymaan.modules.event.validator.EventValidator;
import com.studymaan.modules.study.Study;
import com.studymaan.modules.study.StudyService;
import com.studymaan.modules.event.form.EventForm;
import com.studymaan.modules.tag.Tag;
import com.studymaan.modules.zone.Zone;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/{path}")
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentAccount Account account, @PathVariable String path,
                                 @Valid EventForm eventForm, Errors errors, Model model) { // Whitelabel Error Page 문제 : Errors를 Model 보다 앞에 선언하면 해결, Model은 웬만하면 뒤에 선언
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if(errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "/event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), study, account);
        return "redirect:/study/" + study.getStudyPath(path) + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        model.addAttribute(account);
        model.addAttribute("event", eventRepository.findById(id).orElseThrow());
        model.addAttribute("study", studyService.getStudy(path));
        model.addAttribute("eventType", EventType.values());
        return "event/view";
    }

    @GetMapping("/events")
    public String viewStudyEvents(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();

        events.forEach(e -> {
            if(e.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);
        return "study/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        Event event = eventRepository.findById(id).orElseThrow();
        model.addAttribute(account);
        model.addAttribute("study", studyService.getStudyToUpdate(account, path));
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));

        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id,
                                    @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventForm.setEventType(event.getEventType());

        eventValidator.validateUpdateForm(eventForm, event, errors);

        if(errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(eventForm, event);
        return "redirect:/study/" + study.getStudyPath(path) + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/delete")
    public String cancelEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        eventService.deleteEvent(eventRepository.findById(id).orElseThrow());
        return "redirect:/study/" + study.getStudyPath(path) + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(eventRepository.findById(id).orElseThrow(), account);
        return "redirect:/study/" + study.getStudyPath(path) + "/events/" + id;
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.cancelEnrollment(eventRepository.findById(id).orElseThrow(), account);
        return "redirect:/study/" + study.getStudyPath(path) + "/events/" + id;
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.acceptEnrollment(event, enrollment);
        return "redirect:/study/" + study.getStudyPath(path) + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.rejectEnrollment(event, enrollment);
        return "redirect:/study/" + study.getStudyPath(path) + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.checkInEnrollment(enrollment);
        return "redirect:/study/" + study.getStudyPath(path) + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/study/" + study.getStudyPath(path) + "/events/" + event.getId();
    }

}
