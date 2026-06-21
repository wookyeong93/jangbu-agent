<script setup lang="ts">
import type { SelectOption } from '~/types/code'

definePageMeta({ title: '스타일 가이드' })

const modal = useModalStore()

const text = ref('')
const textWithError = ref('')

// 데모용 임시 옵션 — 실제 공통코드(trx_type 등)는 장부 폼 작업에서 codes API로 연동
const DEMO_OPTIONS: SelectOption[] = [
  { value: 'A', label: '옵션 A' },
  { value: 'B', label: '옵션 B' },
  { value: 'C', label: '옵션 C' }
]

const selectValue = ref('A')
const radioValue = ref('A')
const toggleValue = ref(false)
const dateValue = ref('2026-06-20')

async function showAlert() {
  await modal.alert({ title: '안내', message: '저장되었습니다.' })
}

async function showConfirm() {
  const ok = await modal.confirm({
    title: '삭제 확인',
    message: '정말 삭제하시겠습니까?',
    variant: 'danger',
    confirmText: '삭제'
  })
  await modal.alert({ message: ok ? '삭제했습니다.' : '취소했습니다.' })
}
</script>

<template>
  <div class="max-w-md space-y-6">
    <section class="space-y-2">
      <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">Button</h2>
      <div class="flex flex-wrap gap-2">
        <AppButton variant="primary">Primary</AppButton>
        <AppButton variant="secondary">Secondary</AppButton>
        <AppButton variant="danger">Danger</AppButton>
        <AppButton variant="primary" loading>Loading</AppButton>
        <AppButton variant="primary" disabled>Disabled</AppButton>
      </div>
    </section>

    <section class="space-y-4">
      <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">Input</h2>
      <AppInput v-model="text" id="text" label="아이디" placeholder="아이디를 입력하세요" />
      <AppInput
        v-model="textWithError"
        id="text-error"
        label="비밀번호"
        type="password"
        error="비밀번호가 올바르지 않습니다."
      />
    </section>

    <section class="space-y-2">
      <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">SelectBox</h2>
      <AppSelectBox
        v-model="selectValue"
        id="select-demo"
        label="구분"
        :options="DEMO_OPTIONS"
      />
    </section>

    <section class="space-y-2">
      <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">RadioBox</h2>
      <AppRadioGroup
        v-model="radioValue"
        name="radioDemo"
        label="구분"
        :options="DEMO_OPTIONS"
      />
    </section>

    <section class="space-y-2">
      <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">Toggle</h2>
      <AppToggle v-model="toggleValue" label="알림 받기" />
    </section>

    <section class="space-y-2">
      <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">DatePicker</h2>
      <AppDatePicker v-model="dateValue" id="date-demo" label="거래일자" />
    </section>

    <section class="space-y-2">
      <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">Modal</h2>
      <div class="flex flex-wrap gap-2">
        <AppButton variant="secondary" @click="showAlert">Alert 열기</AppButton>
        <AppButton variant="danger" @click="showConfirm">Confirm 열기</AppButton>
      </div>
    </section>
  </div>
</template>
