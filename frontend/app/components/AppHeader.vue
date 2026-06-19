<script setup lang="ts">
defineProps<{
  title?: string
}>()
</script>

<template>
  <header class="flex h-16 items-center justify-between border-b border-[var(--color-border)] bg-[var(--color-surface)] px-6">
    <h1 class="text-base font-semibold text-[var(--color-text)]">
      {{ title }}
    </h1>
    <div class="flex items-center gap-3 text-sm text-[var(--color-text-muted)]">
      <slot name="actions" />
    </div>
  </header>
</template>
