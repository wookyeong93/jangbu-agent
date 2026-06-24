<script setup lang="ts">
defineProps<{
  title?: string
}>()

const auth = useAuthStore()

async function handleLogout() {
  await auth.logout()
  await navigateTo('/login')
}
</script>

<template>
  <header class="flex h-16 items-center justify-between border-b border-[var(--color-border)] bg-[var(--color-surface)] px-6">
    <h1 class="text-base font-semibold text-[var(--color-text)]">
      {{ title }}
    </h1>
    <div class="flex items-center gap-4 text-sm text-[var(--color-text-muted)]">
      <slot name="actions" />
      <span v-if="auth.profile">{{ auth.profile.userNm }}님</span>
      <NuxtLink to="/profile" class="hover:text-[var(--color-text)]">내 정보</NuxtLink>
      <button type="button" class="hover:text-[var(--color-text)]" @click="handleLogout">로그아웃</button>
    </div>
  </header>
</template>
