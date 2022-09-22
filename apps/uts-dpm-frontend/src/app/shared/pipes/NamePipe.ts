import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'name' })
export class NamePipe implements PipeTransform {
  transform(name: string, type: 'first' | 'last'): string {
    return type === 'first' ? this.firstname(name) : this.lastname(name);
  }

  private firstname(name: string): string {
    const index = name.indexOf(' ');
    return index === -1 ? name : name.substring(0, index);
  }

  private lastname(name: string): string {
    const index = name.indexOf(' ');
    return index === -1 ? '' : name.substring(index).trim();
  }
}
