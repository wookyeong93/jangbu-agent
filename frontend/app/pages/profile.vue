<script setup lang="ts">
definePageMeta({ title: '내 정보' })

const auth = useAuthStore()
const modal = useModalStore()

const userNm = ref('')
const nameLoading = ref(false)

const currentPassword = ref('')
const newPassword = ref('')
const passwordLoading = ref(false)

onMounted(async () => {
  if (!auth.profile) {
    await auth.fetchProfile()
  }
  userNm.value = auth.profile?.userNm ?? ''
})

async function handleUpdateName() {
  nameLoading.value = true
  try {
    await auth.updateProfile({ userNm: userNm.value })
    await modal.alert({ title: '저장 완료', message: '이름이 변경되었습니다.' })
  } catch (err) {
    const message = err instanceof Error ? err.message : '이름 변경에 실패했습니다.'
    await modal.alert({ title: '변경 실패', message })
  } finally {
    nameLoading.value = false
  }
}

async function handleUpdatePassword() {
  passwordLoading.value = true
  try {
    await auth.updateProfile({ currentPassword: currentPassword.value, newPassword: newPassword.value })
    currentPassword.value = ''
    newPassword.value = ''
    await modal.alert({ title: '저장 완료', message: '비밀번호가 변경되었습니다.' })
  } catch (err) {
    const message = err instanceof Error ? err.message : '비밀번호 변경에 실패했습니다.'
    await modal.alert({ title: '변경 실패', message })
  } finally {
    passwordLoading.value = false
  }
}
</script>

<template>
  <div class="max-w-md space-y-8">
    <section class="space-y-4 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-6">
      <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">기본 정보</h2>
      <AppInput :model-value="auth.profile?.userId ?? ''" id="profile-userId" label="아이디" disabled />
      <AppInput v-model="userNm" id="profile-userNm" label="이름" placeholder="이름을 입력하세요" />
      <AppButton :loading="nameLoading" @click="handleUpdateName">이름 저장</AppButton>
    </section>

    <section class="space-y-4 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-6">
      <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">비밀번호 변경</h2>
      <AppInput
        v-model="currentPassword"
        id="profile-currentPassword"
        name="currentPassword"
        type="password"
        label="현재 비밀번호"
        autocomplete="current-password"
      />
      <AppInput
        v-model="newPassword"
        id="profile-newPassword"
        name="newPassword"
        type="password"
        label="새 비밀번호"
        autocomplete="new-password"
      />
      <AppButton :loading="passwordLoading" @click="handleUpdatePassword">비밀번호 변경</AppButton>
    </section>
  </div>
</template>
