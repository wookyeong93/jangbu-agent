<script setup lang="ts">
withDefaults(
  defineProps<{
    variant?: 'primary' | 'secondary' | 'danger'
    type?: 'button' | 'submit'
    disabled?: boolean
    loading?: boolean
  }>(),
  {
    variant: 'primary',
    type: 'button',
    disabled: false,
    loading: false
  }
)

const VARIANT_CLASS: Record<string, string> = {
  primary: 'bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-hover)]',
  secondary: 'bg-white text-[var(--color-text)] border border-[var(--color-border)] hover:bg-gray-50',
  danger: 'bg-red-600 text-white hover:bg-red-700'
}
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    class="inline-flex items-center justify-center gap-2 rounded-md px-4 py-2 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50"
    :class="VARIANT_CLASS[variant]"
  >
    <span v-if="loading" class="h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent" />
    <slot />
  </button>
</template>
