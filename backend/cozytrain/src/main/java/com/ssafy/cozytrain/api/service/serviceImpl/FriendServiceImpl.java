package com.ssafy.cozytrain.api.service.serviceImpl;

import com.ssafy.cozytrain.api.dto.FriendDto;
import com.ssafy.cozytrain.api.dto.TrainDto;
import com.ssafy.cozytrain.api.entity.ChatRoom;
import com.ssafy.cozytrain.api.entity.Friend;
import com.ssafy.cozytrain.api.entity.Member;
import com.ssafy.cozytrain.api.entity.elastic.MemberCompleteDocument;
import com.ssafy.cozytrain.api.repository.ChatRoomRepository;
import com.ssafy.cozytrain.api.repository.FriendRepository;
import com.ssafy.cozytrain.api.repository.MemberRepository;
import com.ssafy.cozytrain.api.repository.MessageRepository;
import com.ssafy.cozytrain.api.repository.elastic.MemberCompleteRepository;
import com.ssafy.cozytrain.api.service.FriendService;
import com.ssafy.cozytrain.api.service.TrainService;
import com.ssafy.cozytrain.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberCompleteRepository memberCompleteRepository;
    private final MessageRepository messageRepository;
    private final TrainService trainService;

    @Override
    public List<FriendDto.FriendSearchResDto> searchFriend(String memberId, String friendLoginId) {
        Member member = memberRepository.findByMemberLoginId(memberId).orElseThrow(() -> {
            log.info("해당 User에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found User");
        });

        // elastic search를 이용하여 친구 로그인 ID들 받아오기
        List<MemberCompleteDocument> friendList = memberCompleteRepository.findByMemberName(friendLoginId);
        List<String> friendLoginIdList = new ArrayList<>(); // LoginId만 따로 저장
        friendList.forEach(e -> {
            friendLoginIdList.add(e.getMemberName());
        });

        List<FriendDto.FriendSearchResDto> friendAllList = friendRepository.searchFriend(member.getMemberId(), friendLoginIdList).orElseThrow(() -> {
            log.info("검색된 친구의 정보를 찾지 못했습니다.");
            return new NotFoundException("검색한 친구가 없습니다");
        });

        friendAllList.forEach(e -> {
            Member friendMember = memberRepository.findByMemberId(e.getMemberId()).orElseThrow(() -> {
                log.info("해당 User에 대한 정보를 찾지 못했습니다.");
                return new NotFoundException("Not Found User");
            });
            TrainDto.TrainCurInfoDto friendTrain = trainService.getCurLocationInfo(friendMember);
            e.setTrainInfo(friendTrain);
        });

        return friendAllList;
    }

    @Override
    @Transactional
    public Long createFriend(String memberId, FriendDto.FriendReqDto friendReqDto) {
        Long friendId = friendReqDto.getMemberId();
        Member member = memberRepository.findByMemberLoginId(memberId).orElseThrow(() -> {
            log.info("해당 User에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found User");
        });

        int friendType = 0;
        Long memberFirstId = member.getMemberId();
        Long memberSecondId = friendId;

        if (member.getMemberId() > friendId) {
            memberFirstId = friendId;
            memberSecondId = member.getMemberId();
            friendType = 1;
        }

        Member firstMember = memberRepository.findByMemberId(memberFirstId).orElseThrow(() -> {
            log.info("해당 User에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found User");
        });
        Member secondMember = memberRepository.findByMemberId(memberSecondId).orElseThrow(() -> {
            log.info("해당 User에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found User");
        });


        Friend friend = Friend.builder()
                .memberFirst(firstMember)
                .memberSecond(secondMember)
                .friendType(friendType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return friendRepository.save(friend).getFriendId();
    }

    @Override
    @Transactional
    public Long acceptFriend(String memberId, FriendDto.FriendAcceptReqDto friendAcceptReqDto) {
        Member member = memberRepository.findByMemberLoginId(memberId).orElseThrow(() -> {
            log.info("해당 User에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found User");
        });

        Friend friend = friendRepository.findById(friendAcceptReqDto.getFriendId()).orElseThrow(() -> {
            log.info("해당 친구요청에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found request Friend");
        });
        friend.updateFriendType(2);
        friend.updateUpdatedAt();
        friendRepository.save(friend).getFriendId();

        Long friendId = friend.getMemberFirst().getMemberId();
        if (friendId == member.getMemberId()) {
            friendId = friend.getMemberSecond().getMemberId();
        }

        Member memberSecond = memberRepository.findByMemberId(friendId).orElseThrow(() -> {
            log.info("해당 User에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found User");
        });

        ChatRoom chatRoom = ChatRoom.builder()
                .memberFirst(member)
                .memberSecond(memberSecond)
                .build();
        return chatRoomRepository.save(chatRoom).getChatRoomId();
    }

    @Override
    @Transactional
    public Long deleteFriend(Long friendId) {
        return friendRepository.deleteByFriendId(friendId);
    }

    @Override
    @Transactional
    public List<FriendDto.FriendResDto> getFriendList(String memberId) {
        Member member = memberRepository.findByMemberLoginId(memberId).orElseThrow(() -> {
            log.info("해당 User에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found User");
        });

        List<FriendDto.FriendResDto> friendList = friendRepository.getFriendList(member.getMemberId()).orElseThrow(() -> {
            log.info("친구 목록을 불러올 수 없습니다.");
            return new NotFoundException("Not Found Friend List");
        });

        friendList.forEach(e -> {
            ChatRoom chatRoom = chatRoomRepository.findByMemberFirst_MemberIdAndMemberSecond_MemberIdOrMemberSecond_MemberIdAndMemberFirst_MemberId
                            (member.getMemberId(), e.getMemberId(), member.getMemberId(), e.getMemberId())
                    .orElseThrow(() -> {
                        log.info("해당 친구와의 채팅방 정보를 찾지 못했습니다.");
                        return new NotFoundException("Not Found ChatRoom");
                    });
            e.setChatRoomId(chatRoom.getChatRoomId());

            Member friendMember = memberRepository.findByMemberId(e.getMemberId()).orElseThrow(() -> {
                log.info("해당 User에 대한 정보를 찾지 못했습니다.");
                return new NotFoundException("Not Found User");
            });
            TrainDto.TrainCurInfoDto friendTrain = trainService.getCurLocationInfo(friendMember);
            e.setTrainInfo(friendTrain);

            int noReadCo = messageRepository.countByChatRoom_ChatRoomIdAndSenderMember_MemberIdNotAndIsRead(chatRoom.getChatRoomId(), member.getMemberId(), 0);
            e.setNoReadCo(noReadCo);
        });

        Collections.sort(friendList, new Comparator<FriendDto.FriendResDto>() {
            @Override
            public int compare(FriendDto.FriendResDto o1, FriendDto.FriendResDto o2) {
                return o2.getTrainInfo().getTotalDist()- o1.getTrainInfo().getTotalDist();
            }
        });

        return friendList;
    }

    @Override
    @Transactional
    public List<FriendDto.FriendResDto> getSentRequestList(String memberId) {
        Member member = memberRepository.findByMemberLoginId(memberId).orElseThrow(() -> {
            log.info("해당 User에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found User");
        });

        List<FriendDto.FriendResDto> friendList = friendRepository.getSentRequestList(member.getMemberId()).orElseThrow(() -> {
            log.info("보낸 친구 요청 목록을 찾지 못했습니다.");
            return new NotFoundException("Not Found Send Friend List");
        });

        friendList.forEach(e -> {
            Member friendMember = memberRepository.findByMemberId(e.getMemberId()).orElseThrow(() -> {
                log.info("해당 User에 대한 정보를 찾지 못했습니다.");
                return new NotFoundException("Not Found User");
            });
            TrainDto.TrainCurInfoDto friendTrain = trainService.getCurLocationInfo(friendMember);
            e.setTrainInfo(friendTrain);
        });

        return friendList;
    }

    @Override
    @Transactional
    public List<FriendDto.FriendResDto> getReceivedRequestList(String memberId) {
        Member member = memberRepository.findByMemberLoginId(memberId).orElseThrow(() -> {
            log.info("해당 User에 대한 정보를 찾지 못했습니다.");
            return new NotFoundException("Not Found User");
        });
        List<FriendDto.FriendResDto> friendList = friendRepository.getReceivedRequestList(member.getMemberId()).orElseThrow(() -> {
            log.info("받은 친구 요청 목록을 찾지 못했습니다.");
            return new NotFoundException("Not Found Received Friend List");
        });

        friendList.forEach(e -> {
            Member friendMember = memberRepository.findByMemberId(e.getMemberId()).orElseThrow(() -> {
                log.info("해당 User에 대한 정보를 찾지 못했습니다.");
                return new NotFoundException("Not Found User");
            });
            TrainDto.TrainCurInfoDto friendTrain = trainService.getCurLocationInfo(friendMember);
            e.setTrainInfo(friendTrain);
        });

        return friendList;
    }
}
