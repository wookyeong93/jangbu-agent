<script setup lang="ts">
const modal = useModalStore()
const dialogRef = ref<HTMLDivElement | null>(null)

watch(
  () => modal.isOpen,
  async (open) => {
    if (open) {
      await nextTick()
      dialogRef.value?.focus()
    }
  }
)
</script>

<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div
        v-if="modal.isOpen"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
        @keydown.esc="modal.handleCancel"
      >
        <div ref="dialogRef" class="w-full max-w-sm rounded-lg bg-[var(--color-surface)] p-6 shadow-lg" tabindex="-1">
          <h2 v-if="modal.options.title" class="text-base font-semibold text-[var(--color-text)]">
            {{ modal.options.title }}
          </h2>
          <p class="mt-2 text-sm text-[var(--color-text-muted)]">
            {{ modal.options.message }}
          </p>
          <div class="mt-6 flex justify-end gap-2">
            <AppButton
              v-if="modal.options.mode === 'confirm'"
              variant="secondary"
              @click="modal.handleCancel"
            >
              {{ modal.options.cancelText ?? '취소' }}
            </AppButton>
            <AppButton
              :variant="modal.options.variant === 'danger' ? 'danger' : 'primary'"
              @click="modal.handleConfirm"
            >
              {{ modal.options.confirmText ?? '확인' }}
            </AppButton>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.15s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
