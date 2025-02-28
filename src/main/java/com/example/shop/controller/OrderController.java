package com.example.shop.controller;

import com.example.shop.dto.OrderDTO;
import com.example.shop.exception.OutOfStockException;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity order(@RequestBody @Valid OrderDTO orderDTO,
                                BindingResult bindingResult, Principal principal){

        log.info("주문하기 post 진입, 여기까진 됨");
        log.info("들어온 값 체크 : " + orderDTO);

        if(bindingResult.hasErrors()){
            log.info("유효성검사 count가 없다면");
            StringBuilder sb = new StringBuilder();
            //"홍길동" + "홍길동" append를 통해 추가적으로 저장 가능

            log.info(bindingResult.getAllErrors());

            List<FieldError> fieldErrorList =
                bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrorList){
                sb.append(fieldError.getDefaultMessage());
            }

            log.info(sb.toString());

            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        if(principal == null){
            log.info("로그인 안 되어있음");
            return new ResponseEntity<String>("", HttpStatus.UNAUTHORIZED);
        }
        //주문을 하려면 부모인 주문엔티티 필요, 주문 엔티티는 회원과 1:1, 이메일로 주문을 찾아온다
        String email = principal.getName();


        Long orderId = null;
        //수량 부족에 대한 예외처리
        try {
            orderId = orderService.order(orderDTO, email);

            //주문 아이템의 부모인 아이템 > 입력받은 itemid로 해결
        }catch (OutOfStockException e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(orderId, HttpStatus.OK);

    }
}
