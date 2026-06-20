<script setup lang="ts">
definePageMeta({ title: '스타일 가이드' })

const text = ref('')
const textWithError = ref('')
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
  </div>
</template>
