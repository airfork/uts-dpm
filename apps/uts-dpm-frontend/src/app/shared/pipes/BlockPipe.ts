import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'block' })
export class BlockPipe implements PipeTransform {
  transform(block: string): string {
    if (!block.startsWith('[')) block = '[' + block;
    if (!block.endsWith(']')) block += ']';
    return block;
  }
}
