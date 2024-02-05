package com.d101.frientree.serviceImpl;

import com.d101.frientree.dto.leaf.request.LeafGenerationRequest;
import com.d101.frientree.dto.leaf.response.LeafComplaintResponse;
import com.d101.frientree.dto.leaf.response.LeafConfirmationResponse;
import com.d101.frientree.dto.leaf.response.LeafGenerationResponse;
import com.d101.frientree.dto.leaf.response.LeafViewResponse;
import com.d101.frientree.dto.leaf.response.dto.LeafConfirmationResponseDTO;
import com.d101.frientree.dto.message.response.MessageResponse;
import com.d101.frientree.entity.LeafCategory;
import com.d101.frientree.entity.leaf.LeafDetail;
import com.d101.frientree.entity.leaf.LeafReceive;
import com.d101.frientree.entity.leaf.LeafSend;
import com.d101.frientree.entity.user.User;
import com.d101.frientree.exception.leaf.LeafNotFoundException;
import com.d101.frientree.exception.user.UserNotFoundException;
import com.d101.frientree.repository.*;
import com.d101.frientree.service.LeafService;
import com.d101.frientree.service.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeafServiceImpl implements LeafService {

    private final LeafRepository leafRepository;
    private final LeafSendRepository leafSendRepository;
    private final LeafReceiveRepository leafReceiveRepository;
    private final UserRepository userRepository;
    private final LeafDetailRepository leafDetailRepository;
    private final MessageService messageService;

    @Override
    public ResponseEntity<LeafConfirmationResponse> confirm(String leafCategory) {
        User currentUser = getUser();
        Long userId = currentUser.getUserId();

        // 1. leaf_send와 leaf_receive 테이블에서 현재 로그인한 사용자가 보낸 및 받은 leaf_num 가져오기
        List<Long> sentAndReceivedLeafNums = new ArrayList<>();
        sentAndReceivedLeafNums.addAll(leafSendRepository.findSentLeafNumsByUserId(userId));
        sentAndReceivedLeafNums.addAll(leafReceiveRepository.findReceivedLeafNumsByUserId(userId));

        // 2. leaf_detail 테이블에서 leaf_category에 해당하는 이파리 중에서
        //    로그인한 사용자가 보낸 및 받은 leaf를 제외한 이파리들 가져오기
        LeafCategory selectedCategory = LeafCategory.valueOf(leafCategory.toUpperCase());

        Optional<LeafDetail> leaves;

        if (sentAndReceivedLeafNums.isEmpty()) {
            // 선택한 카테고리에 해당하는 모든 LeafDetail 가져오기
            leaves = leafRepository.findByLeafCategoryOrderByLeafViewAsc(selectedCategory).stream().findFirst();
        } else {
            // 선택한 카테고리에 속하면서 sentAndReceivedLeafNums에 포함되지 않은 LeafDetail 가져오기
            leaves = leafRepository.findAllByLeafCategoryAndLeafNumNotInOrderByLeafViewAsc(selectedCategory, sentAndReceivedLeafNums).stream().findFirst();
        }

        Optional<LeafDetail> selectedorderbyLeaf = leaves;


        // Optional을 사용하니까 isPresent 가 사용이 가능해서 코드 수정했습니다.
        if (selectedorderbyLeaf.isPresent()) {
            // 선택된 leaf의 leaf_view 값을 1 증가
            LeafDetail selectedLeaf = selectedorderbyLeaf.get();
            selectedLeaf.setLeafView(selectedLeaf.getLeafView() + 1);

            // leaf 업데이트
            leafRepository.save(selectedLeaf);

            LeafConfirmationResponse response = LeafConfirmationResponse.createLeafConfirmationResponse(
                    "Success",
                    LeafConfirmationResponseDTO.createLeafConfirmationResponseDTO(selectedLeaf)
            );
            return ResponseEntity.ok(response);
        }

        // 더 이상 받을 이파리가 없을 때 MessageResponse의 description을 가져와서 LeafConfirmationResponse 형식으로 반환
        ResponseEntity<MessageResponse> messageResponseEntity = messageService.confirm();
        MessageResponse messageResponse = messageResponseEntity.getBody();

        if (messageResponse != null) {
            String description = messageResponse.getData();

            // LeafConfirmationResponse 객체 생성
            LeafConfirmationResponse response = LeafConfirmationResponse.createLeafConfirmationResponse(
                    "Success",
                    LeafConfirmationResponseDTO.createLeafConfirmationResponseDTO(description)  // 또는 description를 이용하여 DTO를 생성
            );
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Override
    @Transactional
    public ResponseEntity<LeafGenerationResponse> generate(LeafGenerationRequest leafGenerationRequest) {

        // user 정보를 받아올 때, 유효성 검사까지 함께하는 메서드를 가지고 와서 사용했습니다.
        User currentUser = getUser();

        LocalDateTime leafCreateDate = LocalDateTime.now();

        LeafDetail newLeaf = LeafDetail.createLeafDetail(leafGenerationRequest);
        newLeaf.setLeafCreateDate(Date.from(leafCreateDate.atZone(ZoneId.systemDefault()).toInstant()));

        // LeafDetail 저장
        leafRepository.save(newLeaf);


        LeafSend leafSend = LeafSend.createLeafSend(newLeaf, currentUser);

        // LeafSend 테이블에 추가
        leafSendRepository.save(leafSend);

        // LeafGenerationResponse 생성
        LeafGenerationResponse response = LeafGenerationResponse.createLeafGenerationResponse(
                "Success",
                true
        );

        // LeafGenerationResponse 반환
        return ResponseEntity.ok(response);

    }

    @Override
    @Transactional
    public ResponseEntity<LeafComplaintResponse> complain(Long leafId) {
        LeafDetail currentLeaf = leafRepository.findById(leafId)
                .orElseThrow(() -> new LeafNotFoundException("이파리가 존재하지 않습니다."));

        currentLeaf.setLeafComplain(currentLeaf.getLeafComplain() + 1);

        // 누적된 complain(신고)수가 5를 충족하면 이파리를 삭제
        if (currentLeaf.getLeafComplain() >= 5) {
            // leaf_send 에 저장된 이파리 삭제
            leafSendRepository.deleteByLeafDetail(currentLeaf);

            // leaf_receive 에 저장된 이파리 삭제
            List<LeafReceive> relatedReceives = leafReceiveRepository.findByLeafDetail(currentLeaf);
            leafReceiveRepository.deleteAll(relatedReceives);

            // LeafDetail에 저장된 이파리 삭제
            leafRepository.delete(currentLeaf);
        }

        LeafComplaintResponse response = LeafComplaintResponse.createLeafComplaintResponse(
                "Success",
                true
        );

        return ResponseEntity.ok(response);
    }


    @Override
    @Transactional
    public ResponseEntity<LeafViewResponse> view() {
        // security를 이용해 로그인 정보를 받아옴
        User currentUser = getUser();

        try {
            Long userId = currentUser.getUserId();

            // 1. leaf_send 테이블에서 user_id를 기준으로 leaf_num을 가져오기
            List<Long> leafNumList = leafSendRepository.findLeafNumsByUser(userId);

            // 2. leaf_detail 테이블에서 leaf_num에 해당하는 leaf_view 값 모두 더하기
            long totalLeafView = leafNumList.stream()
                    .mapToLong(leafNum -> {
                        LeafDetail leafDetail = leafDetailRepository.findByLeafNum(leafNum);
                        return leafDetail != null ? leafDetail.getLeafView() : 0;
                    })
                    .sum();

            // LeafViewResponse를 생성하고 반환
            LeafViewResponse response = LeafViewResponse.createLeafViewResponse(
                    "Success",
                    Long.valueOf(totalLeafView));

            // response 반환
            return ResponseEntity.ok(response);

        } catch (LeafNotFoundException e) {
            // leaf_num을 찾지 못한 경우 ( leaf_send 테이블에 로그인한 유저가 보낸 이파리가 없을 때)
            throw new LeafNotFoundException("송신한 이파리를 찾을 수 없습니다.");
        }
    }


    private User getUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        User currentUser = userRepository.findById(Long.valueOf(userId)
                )
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        if (currentUser.getUserDisabled()) {
            throw new UserNotFoundException("user disabled");
        }

        return currentUser;
    }
}