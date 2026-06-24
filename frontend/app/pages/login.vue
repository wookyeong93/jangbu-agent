<script setup lang="ts">
definePageMeta({ layout: 'auth' })

const userId = ref('')
const password = ref('')
const loading = ref(false)

const auth = useAuthStore()
const modal = useModalStore()

async function handleSubmit() {
  loading.value = true
  try {
    await auth.login(userId.value, password.value)
    await navigateTo('/dashboard')
  } catch (err) {
    const message = err instanceof Error ? err.message : '로그인에 실패했습니다.'
    await modal.alert({ title: '로그인 실패', message })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form class="space-y-6" @submit.prevent="handleSubmit">
    <h1 class="text-lg font-semibold text-[var(--color-text)]">로그인</h1>

    <div class="space-y-4">
      <AppInput
        v-model="userId"
        id="login-userId"
        name="userId"
        label="아이디"
        placeholder="아이디를 입력하세요"
        autocomplete="username"
      />
      <AppInput
        v-model="password"
        id="login-password"
        name="password"
        type="password"
        label="비밀번호"
        placeholder="비밀번호를 입력하세요"
        autocomplete="current-password"
      />
    </div>

    <AppButton type="submit" class="w-full" :loading="loading">로그인</AppButton>
  </form>
</template>
