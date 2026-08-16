package com.example.evcharging.payment.domain;
import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
class PaymentStateMachineTest {@Test void successIsTerminal(){assertThat(PaymentStateMachine.terminal(PaymentStatus.SUCCESS)).isTrue();assertThat(PaymentStateMachine.canSucceed(PaymentStatus.UNKNOWN)).isTrue();assertThat(PaymentStateMachine.canSucceed(PaymentStatus.CLOSED)).isFalse();}}
