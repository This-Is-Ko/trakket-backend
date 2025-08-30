//package org.trakket.service;
//
//import org.trakket.model.EventComment;
//import org.trakket.repository.EventCommentRepository;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EventCommentService {
//
//    private final EventCommentRepository repository;
//
//    public EventCommentService(EventCommentRepository repository) {
//        this.repository = repository;
//    }
//
//    public EventComment addComment(EventComment comment) {
//        return repository.save(comment);
//    }
//
////    public EventComment upvote(Long id) {
////        EventComment comment = repository.findById(id).orElseThrow();
////        comment.setUpvotes(comment.getUpvotes() + 1);
////        return repository.save(comment);
////    }
////
////    public EventComment downvote(Long id) {
////        EventComment comment = repository.findById(id).orElseThrow();
////        comment.setDownvotes(comment.getDownvotes() + 1);
////        return repository.save(comment);
////    }
//}
