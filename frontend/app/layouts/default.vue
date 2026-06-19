<script setup lang="ts">
const route = useRoute()
const pageTitle = computed(() => (route.meta.title as string) ?? '')
</script>

<template>
  <div class="flex min-h-screen bg-[var(--color-bg)]">
    <AppSidebar />
    <div class="flex flex-1 flex-col">
      <AppHeader :title="pageTitle" />
      <main class="flex-1 p-6">
        <slot />
      </main>
    </div>
  </div>
</template>
