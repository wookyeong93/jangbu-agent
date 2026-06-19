<script setup lang="ts">
import { NAV_ITEMS } from '~/constants/nav'

const route = useRoute()
</script>

<template>
  <aside class="flex h-screen w-60 flex-col border-r border-[var(--color-border)] bg-[var(--color-surface)]">
    <div class="flex h-16 items-center px-6 text-lg font-semibold text-[var(--color-text)]">
      장부 에이전트
    </div>
    <nav class="flex-1 space-y-1 px-3">
      <NuxtLink
        v-for="item in NAV_ITEMS"
        :key="item.to"
        :to="item.to"
        class="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors"
        :class="route.path.startsWith(item.to)
          ? 'bg-blue-50 text-[var(--color-primary)]'
          : 'text-[var(--color-text-muted)] hover:bg-gray-50 hover:text-[var(--color-text)]'"
      >
        <span aria-hidden="true">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </NuxtLink>
    </nav>
  </aside>
</template>
